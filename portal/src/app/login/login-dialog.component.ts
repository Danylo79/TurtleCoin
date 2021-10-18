import {Component, OnInit, ViewChild} from '@angular/core';
import {ConfigService} from "../services/config.service";

@Component({
  selector: 'app-login',
  templateUrl: `./login-dialog.component.html`,
  styleUrls: ['./login-dialog.component.css']
})
export class LoginDialogComponent implements OnInit {
  public backend: string;
  public frontend: string;

  constructor(private configService: ConfigService) {
    this.backend = configService.getDataHost();
    this.frontend = configService.getPortalHost();
  }

  ngOnInit(): void {

  }
}
