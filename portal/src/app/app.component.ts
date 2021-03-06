import {Component, OnInit} from '@angular/core';
import {LoginDialogComponent} from './login/login-dialog.component';
import {MatDialog} from "@angular/material/dialog";
import {WalletService} from "./services/wallet.service";
import {Wallet} from "./data/entity/wallet";
import {SendDialogComponent} from "./send/send-dialog.component";
import {AdminService} from "./services/admin.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: []
})

export class AppComponent implements OnInit {
  title = 'ClassroomCoin';

  public wallet: Wallet = new Wallet();
  public wallets: Wallet[] = [];

  constructor(public dialog: MatDialog, private walletService: WalletService, private adminService: AdminService) {
  }


  openLoginDialog(): void {
    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: '250px',
      data: {},
      disableClose: true,
      hasBackdrop: false
    });
  }

  openSendDialog() {
    const dialogRef = this.dialog.open(SendDialogComponent, {
      width: '250px',
      data: {
        wallet: this.wallet
      },
      hasBackdrop: false
    });
  }

  ngOnInit(): void {
    let cookie = this.getCookie("turtle-cookie");
    if (cookie == "") {
      this.openLoginDialog();
    } else {
      let split: string[] = cookie.split("-");
      this.walletService.getWallet(split[0], split[1], split[2]).then(wallet => {
        this.wallet = wallet;
        console.log("Got " + wallet.username);
      });
      this.adminService.getWallets().then(wallets => {
        this.wallets = wallets;
      });
    }
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
