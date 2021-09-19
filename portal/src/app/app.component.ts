import {Component, OnInit} from '@angular/core';
import {Coin} from "./data/coin";
import {Status} from "./data/status";
import {Wallet} from "./data/wallet/wallet";

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

  coins: Coin[] = [
    new Coin("48ef10c98bef26f173161eae4625af42", this.statuses[0]),
    new Coin("24ad1a5b3ff2e6b8702ccbbc4a79c252", this.statuses[1]),
    new Coin("9e31d65d3c1cfd6f00108cf00e13641c", this.statuses[2])
  ];

  wallet: Wallet = new Wallet("danylo.komisarenko", "oNAxLmav", 710, 14);

  ngOnInit(): void {
    // const reader = this.wallet.fetch();
    // console.log(reader);
    //   for (let i = 0; i < reader.coins.length; i++) {
    //       this.coins.push(new Coin(reader.coins[i], this.statuses[0]));
    //   }
  }
}
