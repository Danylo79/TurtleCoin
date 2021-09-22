export class WalletReader {
  public username: string = "";
  public pin: string = "";
  public homeroom: number = -1;
  public studentNumber: number = -1;
  public jobs: string[] = [];
  public coins: string[] = [];

  constructor(res: any) {
    this.username = res.username;
    this.pin = res.pin;
    this.homeroom = res.homeroom;
    this.studentNumber = res.studentNumber;
    this.jobs = res.jobs;
    this.coins = res.coins;
  }
}
