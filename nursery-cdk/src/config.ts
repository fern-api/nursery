export interface NurseryInfraConfig {
  readonly stackName: string;
  readonly accountNumber: string;
  readonly region: string;
  readonly vpcId: string;
  readonly ecsClusterName: string;
  readonly logGroupName: string;
  readonly route53HostedZoneId: string;
  readonly route53HostedZoneName: string;
  readonly domainName: string;
  readonly certificateArn: string;

  // TODO: Should support setting up database if it doesn't exist already
  readonly databaseConfig: ExistingDatabaseConfig;
}

export interface ExistingDatabaseConfig {
  postgresHostname: string;
  postgresUsername: string;
  postgresPassword: string;
}
