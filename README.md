On-Demand Micro Insurance Platform (OD-MIP)

A Team Project

Overview

The On-Demand Micro Insurance Platform (OD-MIP) is a cloud-native,
microservices-based application that enables customers to purchase
short-term, usage-based insurance for products and services such as
travel, mobile devices, rental vehicles, laptops, and other assets.
Unlike traditional insurance products, users can activate coverage for
minutes, hours, days, or weeks, paying only for the duration they need.
---
Business Problem
Traditional insurance policies are often expensive, rigid, and designed
for long-term coverage. Customers who require temporary protection
frequently pay for coverage they never use.
OD-MIP addresses this challenge by providing:
Usage-based insurance
Instant policy activation
Dynamic premium calculation
Automated claims processing
AI-assisted fraud detection
Real-time notifications
---
Key Features
User Management
User Registration & Login
JWT Authentication
Role-Based Access Control (User/Admin/Underwriter)
Policy Management
Create On-Demand Policies
Dynamic Policy Templates
Automatic Policy Activation & Expiry
Premium Engine
Time-based Premium Calculation
Risk-based Pricing
Coupon & Discount Support
Claims
Claim Submission
Claim Validation
Claim Status Tracking
Fraud Detection
Rule-based Fraud Detection
AI-assisted Risk Scoring
Notifications
Email Notifications
Policy Expiry Alerts
Claim Updates
Analytics
Active Policies Dashboard
Revenue Analytics
Claims Analytics
Fraud Reports
---
High-Level Architecture
User Interface ↓ API Gateway ↓ Microservices - Authentication Service -
User Service - Policy Service - Premium Service - Claims Service -
Payment Service - Notification Service - Fraud Detection Service -
Analytics Service
Communication: - REST APIs - Apache Kafka
---
Technology Stack
Backend
Java 21
Spring Boot
Spring Security
Spring Data JPA
Hibernate
Database
PostgreSQL
Redis
Messaging
Apache Kafka
Cloud
AWS
Containers
Docker
Kubernetes
DevOps
GitHub Actions
Jenkins
Terraform
Monitoring
Prometheus
Grafana
ELK Stack
Testing
JUnit 5
Mockito
Testcontainers
---
AI Enhancements
Future enhancements include:
AI Policy Recommendation Engine
AI Claims Assistant
Fraud Prediction Models
RAG-based Policy Search
Agentic AI Workflows
Intelligent Document Verification
---
Non-Functional Requirements
High Availability
Scalability
Security
Fault Tolerance
Observability
CI/CD Automation
---
Future Roadmap
Mobile Applications
IoT Integration
Image-based Damage Assessment
Blockchain-based Claim Audit Trail
Predictive Analytics
Multi-cloud Deployment
---
Project Goals
Build a production-ready enterprise Java application
Demonstrate Microservices Architecture
Implement Event-Driven Design with Kafka
Deploy on AWS using Docker and Kubernetes
Showcase AI integration with enterprise applications
---
License
This project is intended for learning, portfolio demonstration, and
enterprise architecture practice.
