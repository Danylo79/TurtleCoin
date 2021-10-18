import { Transaction } from './transaction';

export class Wallet {
  public username: string = '';
  public pin: string = '';
  public homeroom: number = -1;
  public studentNumber: number = -1;
  public jobs: string[] = [];
  public coins: string[] = [];
  public ledger: Transaction[] = [];

  constructor(res?: any) {
    if (typeof res === 'undefined') return;

    this.username = res.username;
    this.pin = res.pin;
    this.homeroom = res.homeroom;
    this.studentNumber = res.studentNumber;
    this.jobs = res.jobs;
    this.coins = res.coins;

    for (let i: number = 0; i < res.transactions.length; i++) {
      const transaction = new Transaction();
      let trn = res.transactions[i];
      transaction.entity = trn.entity;
      transaction.amount = trn.coins;
      transaction.type = trn.type;
      transaction.formattedTimeStamp = trn.formattedTimeStamp;
      this.ledger.push(transaction);
    }
  }

  public isAdmin(): boolean {
    return this.jobs.indexOf('Admin') > -1;
  }
}
