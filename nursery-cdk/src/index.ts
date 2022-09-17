import { App, Stack } from "aws-cdk-lib";
import { readFileSync } from "fs";
import { resolve } from "path";
import { NurseryInfraConfig } from "./config";
import { NurseryCdkStack } from "./nursery-cdk-stack";

export function main(context?: Record<string, string>): void {
  const app = new App({ context });

  const configFileName = app.node.tryGetContext("config");
  if (!configFileName) {
    console.log('Missing "config" context variable');
    console.log("Usage: cdk deploy -c config=my-config.json");
    return;
  }

  const config = JSON.parse(
    readFileSync(resolve(configFileName), "utf-8")
  ) as NurseryInfraConfig;

  const stack = new NurseryCdkStack(app, config);

  app.synth();
}

if (process.argv[1].endsWith("index.ts")) {
  main();
}
