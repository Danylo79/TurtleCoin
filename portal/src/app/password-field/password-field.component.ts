import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';

@Component({
  selector: 'app-password-field',
  templateUrl: './password-field.component.html',
  styleUrls: ['./password-field.component.css']
})
export class PasswordFieldComponent implements OnInit {
  @Input() pin: string = "";
  @ViewChild("field") field: any;
  public shown = false;

  constructor() {}

  ngOnInit(): void {

  }

  toggle() {
    this.shown = !this.shown;
    if (this.shown) {
      this.field.nativeElement.setAttribute('type', 'text');
    } else {
      this.field.nativeElement.setAttribute('type', 'password');
    }
  }
}
