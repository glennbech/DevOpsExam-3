variable "service_name" {
  description = "Name of the Terraform service"
  type = string
  default = "kand2035"
}

variable "iam_role_name" {
  description = "IAM Role name"
  type = string
  default = "kand2035-iam-role"
}

variable "image_identifier" {
  description = "URL for ECR image"
  type = string
  default = "244530008913.dkr.ecr.eu-west-1.amazonaws.com/kand2035:latest"
}

variable "iam_policy_name" {
  description = "IAM Policy name"
  type = string
  default = "kand2035-iam-policy"
}

variable "cpu_units" {
  description = "Number of CPU units reserved for each instance"
  type = string
  default = "256"
}

variable "memory_amount" {
  description = "Amount of memory in MB reserved for each instance"
  type = number
  default = 1024
}

variable "AWS_REGION" {
  type = string
  default = "eu-west-1"
}