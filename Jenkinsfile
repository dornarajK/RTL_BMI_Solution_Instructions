pipeline {
    agent any

    tools {
        maven 'Maven3'
    }

    parameters {
        string(name: 'DOCKERHUB_REPO', defaultValue: '', description: 'Required Docker Hub repository, for example username/rtl-bmi-solution')
    }

    environment {
        JAVA_HOME = 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot'
        PATH = "C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot\\bin;${env.PATH}"
        SONARQUBE_SERVER = 'SonarQubeServer'
        DOCKERHUB_CREDENTIALS_ID = 'DockerHub'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test with Coverage') {
            steps {
                withEnv(['JAVA_HOME=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot', 'PATH+JDK21=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot\\bin']) {
                    bat 'where java & java -version & mvn -version & mvn -B clean verify'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv("${SONARQUBE_SERVER}") {
                        withEnv(['JAVA_HOME=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot', 'PATH+JDK21=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.13.9-hotspot\\bin']) {
                            bat 'where java & java -version & mvn -version & mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.projectKey=BMI_Cal -Dsonar.projectName=BMI_Cal -Dsonar.token=%SONAR_TOKEN%'
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
                    powershell '''
                        $ErrorActionPreference = 'Stop'
                        $reportPath = Join-Path $env:WORKSPACE 'target/sonar/report-task.txt'
                        if (-not (Test-Path $reportPath)) {
                            throw "SonarScanner task report not found: $reportPath"
                        }

                        $report = @{}
                        Get-Content $reportPath | ForEach-Object {
                            if ($_ -match '^([^=]+)=(.*)$') { $report[$matches[1]] = $matches[2] }
                        }
                        $ceTaskUrl = $report['ceTaskUrl']
                        if (-not $ceTaskUrl) { throw 'ceTaskUrl is missing from report-task.txt' }

                        $authBytes = [Text.Encoding]::ASCII.GetBytes("$($env:SONAR_TOKEN):")
                        $headers = @{ Authorization = 'Basic ' + [Convert]::ToBase64String($authBytes) }
                        $task = $null
                        for ($attempt = 1; $attempt -le 120; $attempt++) {
                            $taskResponse = Invoke-RestMethod -Uri $ceTaskUrl -Headers $headers
                            $task = $taskResponse.task
                            Write-Host "SonarQube background task status: $($task.status)"
                            if ($task.status -in @('SUCCESS', 'FAILED', 'CANCELED')) { break }
                            Start-Sleep -Seconds 5
                        }

                        if (-not $task -or $task.status -ne 'SUCCESS') {
                            throw "SonarQube background task did not succeed. Last status: $($task.status)"
                        }
                        if (-not $task.analysisId) { throw 'SonarQube completed without returning an analysisId' }

                        $sonarBaseUrl = [Uri]$ceTaskUrl
                        $qualityGateUrl = "$($sonarBaseUrl.Scheme)://$($sonarBaseUrl.Authority)/api/qualitygates/project_status?analysisId=$($task.analysisId)"
                        $gateResponse = Invoke-RestMethod -Uri $qualityGateUrl -Headers $headers
                        $gateStatus = $gateResponse.projectStatus.status
                        Write-Host "SonarQube Quality Gate: $gateStatus"
                        if ($gateStatus -ne 'OK') {
                            $failedConditions = @($gateResponse.projectStatus.conditions | Where-Object { $_.status -eq 'ERROR' })
                            if ($failedConditions.Count -gt 0) {
                                Write-Host 'Failed SonarQube Quality Gate conditions:'
                                foreach ($condition in $failedConditions) {
                                    Write-Host (" - {0}: actual={1}, {2} threshold={3}" -f $condition.metricKey, $condition.actualValue, $condition.comparator, $condition.errorThreshold)
                                }
                            } else {
                                Write-Host "No condition details were returned. Review: $qualityGateUrl"
                            }
                            throw "SonarQube Quality Gate did not pass: $gateStatus"
                        }
                    '''
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
                    if (!env.DOCKER_IMAGE?.trim()) {
                        error('Docker image name is missing. Check the Build Docker Image stage and DOCKERHUB_REPO parameter.')
                    }
                    try {
                        docker.withRegistry('https://index.docker.io/v1/', env.DOCKERHUB_CREDENTIALS_ID) {
                            def image = docker.image(env.DOCKER_IMAGE)
                            image.push()
                            image.push('latest')
                        }
                    } catch (Exception e) {
                        echo "Docker Hub push failed for ${env.DOCKER_IMAGE}: ${e.message}"
                        echo "Check that Jenkins credential '${env.DOCKERHUB_CREDENTIALS_ID}' is a username/password credential containing your Docker Hub username and access token, and that DOCKERHUB_REPO uses the same namespace with push access."
                        throw e
                    }
                }
            }
        }
    }
}
