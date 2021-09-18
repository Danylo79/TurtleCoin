export class Status {
  public status: string;
  public icon: string;
  public color: string;

  constructor(status: string, icon: string, color: string) {
    this.status = status;
    this.icon = icon;
    this.color = color;
  }
}
