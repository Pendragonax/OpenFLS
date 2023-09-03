import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class Comparer {
  compare(a: number | String, b: number | String, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  compareDates(a: Date, b: Date, isAsc: boolean) {
    console.log(a);
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

}
