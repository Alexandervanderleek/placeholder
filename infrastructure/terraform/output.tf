output "db_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.postgres.endpoint
}

output "ecr_repository_url" {
  description = "ECR Repository URL"
  value       = aws_ecr_repository.app_repo.repository_url
}

output "cli_bucket_name" {
  description = "S3 bucket name for CLI distribution"
  value       = aws_s3_bucket.cli_bucket.bucket
}

output "alb_dns_name" {
  description = "Application Load Balancer DNS name"
  value       = aws_lb.app_lb.dns_name
}
