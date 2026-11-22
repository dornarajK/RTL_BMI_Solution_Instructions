pipeline {
    agent any

    tools {
        maven 'Maven3' // Configure this name in Manage Jenkins > Tools.
    }

    parameters {
        string(name: 'DOCKERHUB_REPO', defaultValue: '', description: 'Required Docker Hub repository, for example username/rtl-bmi-solution')
    }

    environment {
        SONARQUBE_SERVER = 'SonarQubeServer'
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test with Coverage') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/jacoco.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv("${SONARQUBE_SERVER}") {
                        bat 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=BMI_Cal -Dsonar.projectName=BMI_Cal -Dsonar.token=%SONAR_TOKEN%'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Validate Docker Hub Configuration') {
            steps {
                script {
                    if (!params.DOCKERHUB_REPO?.trim() || !(params.DOCKERHUB_REPO ==~ /[a-z0-9]+(?:[._-][a-z0-9]+)*\/[a-z0-9]+(?:[._-][a-z0-9]+)*/)) {
                        error('Set DOCKERHUB_REPO to a valid Docker Hub namespace/repository before running this pipeline.')
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    env.DOCKER_IMAGE = "${params.DOCKERHUB_REPO}:${env.BUILD_NUMBER}"
                    docker.build(env.DOCKER_IMAGE)
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', env.DOCKERHUB_CREDENTIALS_ID) {
                        def image = docker.image(env.DOCKER_IMAGE)
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
    }
}
