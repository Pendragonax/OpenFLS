export class TableButtonCell {
  checked: boolean = false;
  exists: boolean = false;
  enabled: boolean = false;
  content: string = "";
  payload: any | null = null;

  constructor(exists: boolean, checked: boolean, enabled: boolean, content: string, payload: any) {
    this.exists = exists;
    this.checked = checked;
    this.enabled = enabled;
    this.content = content;
    this.payload = payload;
  }
}
