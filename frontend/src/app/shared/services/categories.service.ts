import { Injectable } from '@angular/core';
import {CategoryTemplateDto} from "../dtos/category-template-dto.model";
import {Base} from "./base.service";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CategoriesService extends Base<CategoryTemplateDto> {
  override url = "categories";

  constructor(protected override http: HttpClient) {
    super(http);
    this.initialLoad();
  }

  initialLoad() {
    this.getAll().subscribe(values => {
      this.allValues$.next(values);
    });
  }
}
