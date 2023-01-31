import { Component, OnInit } from '@angular/core';
import {CategoryTemplateDto} from "../../dtos/category-template-dto.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CategoriesService} from "../../services/categories.service";
import {Sort} from "@angular/material/sort";
import {Comparer} from "../../shared/comparer.helper";
import {TablePageComponent} from "../../shared/modules/table-page.component";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.css']
})
export class CategoryComponent extends TablePageComponent<CategoryTemplateDto, CategoryTemplateDto> implements OnInit {
  // CONFIG
  tableColumns = ['name', 'categories', 'actions'];

  constructor(
    override modalService: NgbModal,
    override helperService: HelperService,
    private categoryService: CategoriesService,
    private comparer: Comparer) {
    super(modalService, helperService);
  }

  create(value: CategoryTemplateDto) {
  }

  update(value: CategoryTemplateDto) {
  }

  delete(value: CategoryTemplateDto) {
    this.categoryService
      .delete(value.id)
      .subscribe({
        next: () => this.handleSuccess("Kategorien gelöscht"),
        error: () => this.handleFailure("Fehler beim löschen")
      })
  }

  initFormSubscriptions() {
    this.searchStringControl.valueChanges.subscribe(value => {
      const searchString = value.toLowerCase();
      this.tableSource.data = this.values.filter(value =>
        value.title.toLowerCase().includes(searchString)
        || value.description.toLowerCase().includes(searchString))
    });
  }

  loadValues() {
    this.isSubmitting = true;
    this.categoryService
      .allValues$
      .subscribe({
        next: (values) => {
          this.values = values;
          this.values$.next(values);
          this.filteredTableData = values;

          this.refreshTablePage();
          this.isSubmitting = false;
        },
        error: () => this.handleFailure("Fehler beim laden")
      })
  }

  sortData(sort: Sort) {
    const data = this.tableSource.data.slice();
    if (!sort.active || sort.direction === '') {
      this.tableSource.data = data;
      return;
    }

    this.tableSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      switch (sort.active) {
        case this.tableColumns[0]:
          return this.comparer.compare(a.title, b.title, isAsc);
        default:
          return 0;
      }
    });
  }

  fillEditForm(value: CategoryTemplateDto) {
  }

  filterTableData() {
    this.filteredTableData = this.values.filter(value =>
      value.title.toLowerCase().includes(this.searchString)
      || value.description.toLowerCase().includes(this.searchString)
      || value.categories.some(cat => cat.title.toLowerCase().includes(this.searchString)));

    this.refreshTablePage();
  }

  getNewValue(): CategoryTemplateDto {
    return new CategoryTemplateDto();
  }
}
