import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Base} from './base.service';
import {HourCorridorDto} from '../dtos/hour-corridor-dto.model';

@Injectable({
  providedIn: 'root'
})
export class HourCorridorService extends Base<HourCorridorDto> {
  override url = 'hour_corridors';

  constructor(protected override http: HttpClient) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
      this.allValues = values;
    });
  }
}
