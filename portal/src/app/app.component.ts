import {Component, OnInit} from '@angular/core';
import {Coin} from "./data/coin";
import {Status} from "./data/status";
import {Wallet} from "./data/wallet/wallet";
import {LoginDialogComponent} from './login/login-dialog.component';
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent implements OnInit {
  title = 'ClassroomCoin';
  statuses: Status[] = [
    new Status("Healthy", "done", "accent"),
    new Status("Warning", "warning", "warn"),
    new Status("Error", "dangerous", "warn")
  ];

  coins: Coin[] = [];

  wallet: Wallet = new Wallet("danylo.komisarenko", "oNAxLmav", 710, 14);

  constructor(public dialog: MatDialog) {
  }


  openDialog(): void {
    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: '250px',
      data: {},
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  ngOnInit(): void {
    if (this.getCookie("turtle-cookie") == "") {
      this.openDialog();
    } else {
      this.init();
    }
  }

  init(): void {
    this.wallet.fetch().then((reader) => {
      console.log(reader);
      for (let i = 0; i < reader.coins.length; i++) {
        this.coins.push(new Coin(reader.coins[i], this.statuses[0]));
      }
    });
  }

  private getCookie(name: string) {
    let ca: Array<string> = document.cookie.split(';');
    let caLen: number = ca.length;
    let cookieName = `${name}=`;
    let c: string;

    for (let i: number = 0; i < caLen; i += 1) {
      c = ca[i].replace(/^\s+/g, '');
      if (c.indexOf(cookieName) == 0) {
        return c.substring(cookieName.length, c.length);
      }
    }
    return '';
  }
}
