import {Injectable} from "@angular/core";
import {Wallet} from "../data/entity/wallet";
import {ConfigService} from "./config.service";
import {Status} from "../data/status";

@Injectable({
  providedIn: 'root',
})
export class WalletService {
  statuses: Status[] = [
    new Status("Healthy", "done", "accent"),
    new Status("Warning", "warning", "warn"),
    new Status("Error", "dangerous", "warn")
  ];

  constructor(private configService: ConfigService) {
  }

  getWallet(username: string, roomNumber: string, studentNumber: string): Promise<Wallet> {
    let url = this.configService.getDataHost() + "/wallets/get/" + username + "-" + roomNumber + "-" + studentNumber;
    console.log("Fetching entity for " + username + " using url " + url)
    return fetch(url).then(res => res.json()).then(res => {
      return new Wallet(res);
    });
  }
}
