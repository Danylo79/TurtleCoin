import {Injectable} from "@angular/core";
import {ConfigService} from "./config.service";
import {Wallet} from "../data/entity/wallet";

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  constructor(private configService: ConfigService) {

  }

  getWallets(): Promise<Wallet[]> {
    return fetch(this.configService.getDataHost() + "/wallets/getAll", {credentials: "include"}).then(res => res.json()).then(res => {
      return res.wallets;
    });
  }
}
