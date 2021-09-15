import {Status} from "./status";

export class Coin{
  public hash: string;
  public status: Status;

  constructor(hash: string, status: Status) {
    this.hash = hash;
    this.status = status;
  }
}
