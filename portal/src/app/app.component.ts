import {Component} from '@angular/core';
import {Coin} from "./data/coin";
import {Status} from "./data/status";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ClassroomCoin';
  statuses: Status[] = [
    new Status("Healthy", "done"),
    new Status("Warning", "warning"),
    new Status("Error", "dangerous")
  ];

  coins: Coin[] = [
    new Coin("e2f0d6904de4f3ea86d880faadbd5a87", this.statuses[0]),
    new Coin("c88643c46a2b73a367ce15b82b9992c2", this.statuses[1]),
    new Coin("a2b804615aed6a460114247409968290", this.statuses[2])
  ];
}
