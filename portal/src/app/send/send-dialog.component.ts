import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {Wallet} from "../data/entity/wallet";

@Component({
  selector: 'app-login',
  templateUrl: `./login-dialog.component.html`,
  styleUrls: ['./login-dialog.component.css']
})
export class SendDialogComponent implements OnInit {
  constructor(@Inject(MAT_DIALOG_DATA) public data: {wallet: Wallet}) {}

  ngOnInit(): void {

  }
}
