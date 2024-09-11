import { Component, OnInit } from '@angular/core';
import {ReplaySubject, Subject} from "rxjs";
import {HelperService} from "../services/helper.service";

@Component({
  selector: 'app-detail-page',
  template: ``,
  styleUrls: []
})
export abstract class DetailPageComponent<T> implements OnInit {

  // STATES
  isSubmitting = false;

  // VARs
  value: T = this.getNewValue();
  value$: ReplaySubject<T> = new ReplaySubject<T>();
  editValue: T = this.getNewValue();

  protected constructor(
    protected helperService: HelperService
  ) { }

  ngOnInit(): void {
    this.loadValues();
    this.initFormSubscriptions();
  }

  abstract update();

  abstract getNewValue(): T;

  abstract loadValues();

  abstract refreshForm();

  abstract initFormSubscriptions();

  handleSuccess(message: string) {
    this.helperService.openSnackBar(message);
    this.isSubmitting = false;

    this.loadValues();
  }

  handleFailure(message: string) {
    this.helperService.openSnackBar(message);
    this.isSubmitting = false;
  }
}
