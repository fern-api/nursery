import { Stack, StackProps } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import { Peer, Port, SecurityGroup, Vpc } from "aws-cdk-lib/aws-ec2";
import { Cluster, ContainerImage, LogDriver } from "aws-cdk-lib/aws-ecs";
import { ApplicationLoadBalancedFargateService } from "aws-cdk-lib/aws-ecs-patterns";
import { ApplicationProtocol } from "aws-cdk-lib/aws-elasticloadbalancingv2";
import { LogGroup } from "aws-cdk-lib/aws-logs";
import { HostedZone } from "aws-cdk-lib/aws-route53";
import { Construct } from "constructs";
import { NurseryInfraConfig } from "./config";

const SERVICE_NAME = "nursery";

export class NurseryCdkStack extends Stack {
  constructor(scope: Construct, config: NurseryInfraConfig) {
    super(scope, config.stackName, {
      env: {
        region: config.region,
        account: config.accountNumber,
      },
    });

    const vpc = Vpc.fromLookup(this, "vpc", {
      vpcId: config.vpcId,
    });

    const nurserySg = new SecurityGroup(this, "fdr-sg", {
      securityGroupName: config.stackName,
      vpc,
      allowAllOutbound: true,
    });

    const cluster = Cluster.fromClusterAttributes(this, "cluster", {
      clusterName: config.ecsClusterName,
      vpc,
      securityGroups: [],
    });

    const logGroup = LogGroup.fromLogGroupName(
      this,
      "log-group",
      config.logGroupName
    );

    const certificate = Certificate.fromCertificateArn(
      this,
      "ceritificate",
      config.certificateArn
    );

    const fargateService = new ApplicationLoadBalancedFargateService(
      this,
      SERVICE_NAME,
      {
        serviceName: SERVICE_NAME,
        cluster,
        cpu: 256,
        memoryLimitMiB: 512,
        desiredCount: 1,
        securityGroups: [nurserySg],
        taskImageOptions: {
          image: ContainerImage.fromTarball(`../dockers/nursery-server.tar`),
          environment: {
            MAINTENANCE_JDBC_URL: `jdbc:postgresql://${config.databaseConfig.postgresHostname}:5432/postgres?user=${config.databaseConfig.postgresUsername}&password=${config.databaseConfig.postgresPassword}`,
            JDBC_URL: `jdbc:postgresql://${config.databaseConfig.postgresHostname}:5432/nursery?user=${config.databaseConfig.postgresUsername}&password=${config.databaseConfig.postgresPassword}`,
          },
          containerName: "nursery-server",
          containerPort: 8080,
          enableLogging: true,
          logDriver: LogDriver.awsLogs({
            logGroup,
            streamPrefix: SERVICE_NAME,
          }),
        },
        assignPublicIp: true,
        publicLoadBalancer: true,
        enableECSManagedTags: true,
        protocol: ApplicationProtocol.HTTPS,
        certificate,
        domainZone: HostedZone.fromHostedZoneAttributes(this, "zoneId", {
          hostedZoneId: config.route53HostedZoneId,
          zoneName: config.route53HostedZoneName,
        }),
        domainName: config.domainName,
      }
    );

    fargateService.targetGroup.setAttribute(
      "deregistration_delay.timeout_seconds",
      "30"
    );

    fargateService.targetGroup.configureHealthCheck({
      healthyHttpCodes: "200",
      path: "/health",
      port: "8080",
    });
  }
}
