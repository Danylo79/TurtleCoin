import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {Wallet} from "../data/entity/wallet";
import {ConfigService} from "../services/config.service";

@Component({
  selector: 'app-login',
  templateUrl: `./login-dialog.component.html`,
  styleUrls: ['./login-dialog.component.css']
})
export class SendDialogComponent implements OnInit {
  public backend: string;
  public frontend: string;

  constructor(@Inject(MAT_DIALOG_DATA) public data: {wallet: Wallet}, private configService: ConfigService) {
    this.backend = configService.getDataHost();
    this.frontend = configService.getPortalHost();
  }

  ngOnInit(): void {

  }
}
