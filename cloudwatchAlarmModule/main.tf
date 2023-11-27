provider "aws" {
  region = var.region
}

resource "aws_sns_topic" "alarm_topic" {
  name = "${var.alarm_name}-topic"
}

resource "aws_sns_topic_subscription" "email_subscription" {
  topic_arn = aws_sns_topic.alarm_topic.arn
  protocol  = "email"
  endpoint  = var.email_address
}

resource "aws_cloudwatch_metric_alarm" "custom_metric_alarm" {
  alarm_name          = var.alarm_name
  comparison_operator = "GreaterThanOrEqualToThreshold"
  evaluation_periods  = "1"
  metric_name         = "2035.isUnderage.count"
  namespace           = "kand2035"
  period              = "60"
  statistic           = "Maximum"
  threshold           = var.amount_children_threshold
  alarm_description   = "This metric monitors if any children has been scanned the factory"
  alarm_actions       = [aws_sns_topic.alarm_topic.arn]
}