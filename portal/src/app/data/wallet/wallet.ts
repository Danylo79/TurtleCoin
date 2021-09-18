import {WalletReader} from "./wallet-reader";

export class Wallet {
  public username: string;
  public pin: string;
  public roomNumber: number;
  public studentNumber: number;

  constructor(username: string, pin: string, roomNumber: number, studentNumber: number) {
    this.username = username;
    this.pin = pin;
    this.roomNumber = roomNumber;
    this.studentNumber = studentNumber;
  }

  public fetch(): WalletReader {
    return new WalletReader(fetch("http://localhost:8080/wallets/get/" + this.username + "-" + this.pin + "-" + this.roomNumber + "-" + this.studentNumber).then(res => res.json()));
  }
}
