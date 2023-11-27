package com.example.s3rekognition.controller;

import com.example.s3rekognition.AgeResponse;
import io.micrometer.core.instrument.DistributionSummary;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.*;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.Counter;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {

    private final S3Client s3Client;
    private final RekognitionClient rekognitionClient;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());

    private MeterRegistry meterRegistry;
    private DistributionSummary imageSizeSummary;
    private Counter ppeViolationCounter;
    private Counter underageViolationCounter;

    public RekognitionController(MeterRegistry meterRegistry) {
        this.s3Client = S3Client.builder().region(Region.EU_WEST_1).build();
        this.rekognitionClient = RekognitionClient.builder().region(Region.EU_WEST_1).build();
        this.meterRegistry = meterRegistry;
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Response imageList = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        for (S3Object image : imageList.contents()) {
            // Sends the image size to AWS with Micrometer
            imageSizeSummary.record(image.size());

            // Process each S3Object
            logger.info("scanning " + image.key());
            Image currentImage = Image.builder()
                    .s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder()
                            .bucket(bucketName)
                            .name(image.key())
                            .build())
                    .build();

            DetectProtectiveEquipmentRequest request = DetectProtectiveEquipmentRequest.builder()
                    .image(currentImage)
                    .summarizationAttributes(ProtectiveEquipmentSummarizationAttributes.builder()
                            .minConfidence(80f)
                            .requiredEquipmentTypesWithStrings("FACE_COVER")
                            .build())
                    .build();

            DetectProtectiveEquipmentResponse result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result);

            if (violation) {
                meterRegistry.counter("2035.isViolation").increment();
            }


            logger.info("scanning " + image.key() + ", violation result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.persons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.key(), personCount, violation);
            classificationResponses.add(classification);
        }

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for the age of each person in the image. Reports if any person underage is scanned.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(value = "/check-ages", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public List<AgeResponse> checkAges(@RequestParam String bucketName) {

        // List all objects in the S3 bucket
        ListObjectsV2Response imageList = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());

        List<AgeResponse> ageResponses = new ArrayList<>();

        for (S3Object image : imageList.contents()) {
            logger.info("scanning " + image.key());

            Image currentImage = Image.builder()
                    .s3Object(software.amazon.awssdk.services.rekognition.model.S3Object.builder()
                            .bucket(bucketName)
                            .name(image.key())
                            .build())
                    .build();

            // Sends the image size to AWS with Micrometer
            imageSizeSummary.record(image.size());

            logger.info("Size: " + Long.toString(image.size()));

            DetectFacesRequest request = DetectFacesRequest.builder()
                    .image(currentImage)
                    .attributes(Attribute.AGE_RANGE)
                    .build();

            DetectFacesResponse response = rekognitionClient.detectFaces(request);


            long amntUnderage = response.faceDetails().stream()
                    .filter(face -> face.ageRange().low() < 18)
                    .count();

            if (amntUnderage > 0) {
                underageViolationCounter.increment((double)amntUnderage);
            }

            logger.info("Total: " + Integer.toString(response.faceDetails().size()) +
                    ", Amount less than 18: " + Long.toString(amntUnderage));

            ageResponses.add(new AgeResponse(image.key(), response.faceDetails().size(), amntUnderage));
        }

        return ageResponses;
    }

    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result
     * @return
     */

    private static boolean isViolation(DetectProtectiveEquipmentResponse result) {
        return result.summary().personsWithoutRequiredEquipment().size() > 0;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        this.ppeViolationCounter =  Counter.builder("2035.isViolation")
                .description("If anyone in a picture does not have proper PPE")
                .register(meterRegistry);

        this.underageViolationCounter =  Counter.builder("2035.isUnderage")
                .description("If anyone in a picture is underage")
                .register(meterRegistry);

        this.imageSizeSummary = DistributionSummary.builder("2035.pictureSizes")
                .description("Size of images processed")
                .register(meterRegistry);
    }
}
