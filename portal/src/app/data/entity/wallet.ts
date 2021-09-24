import {Inbound} from "./inbound";
import {Outbound} from "./outbound";

export class Wallet {
  public username: string = "";
  public pin: string = "";
  public homeroom: number = -1;
  public studentNumber: number = -1;
  public jobs: string[] = [];
  public coins: string[] = [];
  public inbound: Inbound[] = [];
  public outbound: Outbound[] = [];

  constructor(res?: any) {
    if (typeof res === "undefined") return;

    this.username = res.username;
    this.pin = res.pin;
    this.homeroom = res.homeroom;
    this.studentNumber = res.studentNumber;
    this.jobs = res.jobs;
    this.coins = res.coins;

    for (let i: number = 0; i < res.inbound.length; i++) {
      this.inbound.push(new Inbound(res.inbound[i].sender, res.inbound[i].coins));
    }

    for (let i: number = 0; i < res.outbound.length; i++) {
      this.outbound.push(new Outbound(res.outbound[i].recipient, res.outbound[i].coins));
    }
  }
}
