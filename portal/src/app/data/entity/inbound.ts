export class Inbound {
  public sender: string;
  public coins: number;

  constructor(sender: string, coins: number) {
    this.sender = sender;
    this.coins = coins;
  }
}
