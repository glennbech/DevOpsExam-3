resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "kand2035"
  dashboard_body = jsonencode(
    {
      "widgets": [
        {
          "type": "metric",
          "x": 0,
          "y": 0,
          "width": 12,
          "height": 6,
          "properties": {
            "metrics": [
              [
                "kand2035",
                "2035.isViolation.count"
              ]
            ],
            "view": "timeSeries",
            "stacked": false,
            "region": "eu-north-1",
            "stat": "Maximum",
            "period": 60,
            "title": "PPE Violations over time"
          }
        },
        {
          "type": "metric",
          "x": 0,
          "y": 12,
          "width": 12,
          "height": 6,
          "properties": {
            "metrics": [
              [
                "kand2035",
                "2035.pictureSizes.sum"
              ]
            ],
            "view": "timeSeries",
            "stacked": false,
            "region": "eu-north-1",
            "stat": "Sum",
            "period": 60,
            "title": "Picture size scanned in bytes over time"
          }
        },
        {
          "type": "metric",
          "x": 12,
          "y": 0,
          "width": 6,
          "height": 6,
          "properties": {
            "metrics": [
              [
                "kand2035",
                "2035.isUnderage.count"
              ]
            ],
            "view": "singleValue",
            "stacked": false,
            "region": "eu-north-1",
            "stat": "Sum",
            "period": 86400,
            "title": "Underage last 24hrs"
          }
        }
      ]
    }
  )
}