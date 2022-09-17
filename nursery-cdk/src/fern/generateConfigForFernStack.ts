// Generates a fern-nursery-config.json which can be used to
// deploy Nursery to Fern's environment.
// If you are an external consumer, this is irrelevant!

import {
  EnvironmentInfo,
  Environments,
} from "@fern-fern/fern-cloud-resources-api-client/model";
import axios from "axios";
import { writeFileSync } from "fs";
import { NurseryInfraConfig } from "../config";

interface EnvironmentVariables {
  awsAccountId: string;
  postgresUsername: string;
  postgresHost: string;
  postgresPassword: string;
}

main();

async function main() {
  const environmentType = process.argv[2];
  const envVars: EnvironmentVariables = {
    awsAccountId: getEnvVarValueOrThrow("AWS_ACCOUNT_ID"),
    postgresUsername: getEnvVarValueOrThrow("POSTGRES_USERNAME"),
    postgresHost: getEnvVarValueOrThrow("POSTGRES_HOST"),
    postgresPassword: getEnvVarValueOrThrow("POSTGRES_PASSWORD"),
  };
  const environmentInfo = await getEnvironments(environmentType);
  const config: NurseryInfraConfig = {
    stackName: `nursery-${environmentType}`,
    accountNumber: envVars.awsAccountId,
    region: "us-east-1",
    vpcId: environmentInfo.vpcId,
    ecsClusterName: environmentInfo.ecsInfo.clusterName,
    logGroupName: environmentInfo.logGroupInfo.logGroupName,
    route53HostedZoneId: environmentInfo.route53Info.hostedZoneId,
    route53HostedZoneName: environmentInfo.route53Info.hostedZoneName,
    domainName: `nursery-dev.buildwithfern.com`,
    certificateArn: environmentInfo.route53Info.certificateArn,
    databaseConfig: {
      postgresHostname: envVars.postgresHost,
      postgresUsername: envVars.postgresUsername,
      postgresPassword: envVars.postgresPassword,
    },
  };
  writeFileSync(`${environmentType}-fern.config.json`, JSON.stringify(config));
}

function getEnvVarValueOrThrow(environmentVariableName: string): string {
  const val = process.env[environmentVariableName];
  if (val != null) {
    return val;
  }
  throw new Error("Missing environment variable: " + environmentVariableName);
}

async function getEnvironments(environment: string): Promise<EnvironmentInfo> {
  const response = await axios(
    "https://raw.githubusercontent.com/fern-api/fern-cloud/main/env-scoped-resources/environments.json",
    {
      method: "GET",
      headers: {
        Authorization: "Bearer " + process.env["GITHUB_TOKEN"],
      },
    }
  );
  const environments = response.data as Environments;
  if (environment === "prod") {
    return environments.PROD;
  } else if (environment === "dev") {
    return environments.DEV;
  }
  throw new Error("Encountered unknown environment: " + environment);
}
