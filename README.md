# Exam PGR301 2023

![Sam-Deploy](https://github.com/ArnoldGonczlik/DevOpsExam/actions/workflows/sam-deploy.yml/badge.svg)
![Docker image to ECR](https://github.com/ArnoldGonczlik/DevOpsExam/actions/workflows/container-ecr.yml/badge.svg)

## What you need to do if you fork this repository
  - Add GitHub repository global secrets for AWS access keys, the user needs at least the following permission in AWS:
    - S3
    - Rekognition
    - Lambda
    - Api
    - Apigateway
    - SNS
    - Apprunner
    - Cloudwatch
    - IAM
  - The two secrets that are needed in GitHub are:
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
- The dashboard created for this task is in eu-north-1 and is named "kand2035"
- The endpoint I chose to add in this task is:
  - /check-ages?bucketName=<name_of_bucket_to_scan>
  - This function checks if any persons below the age of 18 has been scanned in the picture, if yes,
  a counter will increase in the dashboard 
  - I recommend testing this endpoint with bucket: "kand2035", as it contains a stock photo of a child.
- The micrometer instruments I chose were:
  - DistributionSummary
    - This was used to record the size of the pictures scanned in bytes, so they can be graphed.
  - Counter
    - I used two counters, one for all ppeViolations, and the other for all underage persons
      that "have been scanned in the factory".
  - You can see the creation of these three instruments at the bottom in RekognitionController.
- My alarm is for underage persons scanned. It is triggering on 1 or greater. I chose this because
in this hypothetical scenario you don't want anyone underage inside this factory or area. 
Then you will be alerted right away. One of the downsides of me doing this is that I'm not showing my
understanding of evaluation periods in an alarm, where something would trigger if a value holds over time.
- For the alarm email you can either directly change the default value under infra\vars.tf, or you can set
"TF_VAR_email_for_alarm" environment variable.

### Task 4 Drøfteoppgaver:
#### A. Continuous Integration

  - Continuous Integration is a development approach where team members frequently merge their work, often several
times a day. Each merge is automatically tested to detect and fix conflicts early, enhancing code quality.
This automation speeds up the development cycle, making it more efficient and reducing manual work,
thus allowing teams to deliver software more swiftly and reliably.

  - Practically GitHub is used to reduce alot of manual labor with mainly
GitHub Actions (besides the obvious file storing/tracking). 
Here you can from triggers on pushes or merges from other team members get a virtual machine to
execute any piece of code.
For example just like in this exam with docker, terraform, building artifacts and pushing to ECR.

#### B. Scrum/DevOps
 - 1 Scrum/Agile
   - Scrum is a framework used primarily in software development for managing bigger projects.
   It focuses on teamwork, an iterative progress, and adaptability to evolving project needs.
   Within software development, Scrum boosts software quality by organizing tasks into brief, 
   focused periods known as sprints. This structure enables teams to rapidly adjust to new requirements
   and keep improving the product.
   - The biggest strength of Scrum is the flexibility it provides to do revisions and changes when needed while
   still keeping a development structure.
   - The biggest weakness of Scrum is that it's not optimal for smaller or less experienced teams because 
   there is alot of overhead and extra complexity.
   
 - 2 DevOps
   - The founding principles of DevOps are collaboration, automation, CI, quick delivery, 
   feedback and iteration, QA, IaC, monitoring and flexibility.
   - DevOps definitely has a positive impact on the code quality as it allows for a quicker collaboration and
   result driven process.
   - The strengths of DevOps are in the collaboration, time to market and efficiency.
   - The weaknesses of DevOps are in the complexity, skills required and organizational challenges 
   where people are not used to DevOps.
   
 - 3 Comparison
   - I don't think this should be looked at as a contrast, but more how they compliment each other. Many 
   companies use both. Scrum ensures high quality and customer satisfaction while DevOps ensures quick delivery and
   immediate feedback.

#### The Second Way - Feedback
To ensure that the new functionality meets the users needs you can really use feedback to its full potential, 
and DevOps CI makes this very doable. You use CI for quick delivery of new features, which allows for quick 
feedback and faster general development. This process becomes iterative and well tested.
Feedback contributes to the development by identifying users needs, early detection of bugs and a
better user experience.