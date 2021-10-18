import {environment} from "../../environments/environment";

export class ConfigService {
  public getDataHost(): string {
    return environment.dataHost;
  }

  public getPortalHost(): string {
    return environment.portalHost;
  }
}
