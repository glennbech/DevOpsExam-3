# Eksamen PGR301 2023

![Sam-Deploy](https://github.com/ArnoldGonczlik/DevOpsExam/actions/workflows/sam-deploy.yml/badge.svg)
![Docker image to ECR](https://github.com/ArnoldGonczlik/DevOpsExam/actions/workflows/container-ecr.yml/badge.svg)

## What you need to do if you fork this repository
  - Add GitHub repository global secrets for AWS access keys, the user needs at least the following permission in AWS:
    - S3
    - Rekognition
    - Lambda
    - Api
    - Apigateway
    - Sns
    - Apprunner
    - Cloudwatch
    - Iam
  - The two secrets in GitHub needed are:
    - AWS_ACCESS_KEY_ID
    - AWS_SECRET_ACCESS_KEY
  - The variables are:
    - BUCKET_NAME (This will default to "kand2035" if not set, only the sam python app is using this)
    - TF_VAR_email_for_alarm (sets the email the alarm in task4 will alert to, otherwise it defaults
    to example email)

### Task 1 notes:
- The SAM output will be placed in a bucket named "kand2035sam"

### Task 2 notes:
- There is no need to pass BUCKET_NAME as an environment variable to build the dockerfile, 
I assume this line was just copied from task 1 and got overlooked (since you pass bucketnames with url queries). 
So I did not account for any BUCKET_NAME env variables in the java code.

### Task 3 notes:
- No changes required, only the secrets stated above.
- I took the liberty of updating the AWS SDK to version 2 for stability and functionality. This required some code
changes, but they were accounted for.
- I also refactored the pre-existing "isViolation" method as I found a better, more readable way to calculate it.

### Task 4 Feedback notes:
- The endpoint I chose to add in this task is:
  - /check-ages?bucketName=<name_of_bucket_to_scan>
  - This function checks if any persons below the age of 18 has been scanned in the picture, if yes,
  a counter will increase in the dashboard 
  - I recommend testing this endpoint with bucket: "kand2035", as it contains a stock photo of a child.
- The micrometer instruments I chose were:
  - DistributionSummary
    - This was used to record the size of the pictures scanned in bytes, so they can be graphed.
  - Counters
    - I used two counters, one for all ppeViolations, and the other for all underage persons
      that "have been scanned in the factory".
  - You can see the creation of these three instruments at the bottom in RekognitionController.
- My alarm is for underage persons scanned. It is triggering on 1 or greater. I chose this because
in a hypothetical scenario you don't want anyone underage inside this factory or area. 
Then you will be alerted right away. One of the downsides of me doing this is that I'm not showing my
understanding of evaluation periods in an alarm, where something would trigger if a value holds over time.
- For this alarm email you can either directly change the default value under infra\vars.tf, or you can set
"TF_VAR_email_for_alarm" environment variable in GitHub.