export class Outbound {
  public recipient: string;
  public coins: number;

  constructor(recipient: string, coins: number) {
    this.recipient = recipient;
    this.coins = coins;
  }
}
