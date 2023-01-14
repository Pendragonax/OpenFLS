import { Component, OnInit } from '@angular/core';
import {CategoryTemplateDto} from "../../dtos/category-template-dto.model";
import {CategoriesService} from "../../services/categories.service";
import {ActivatedRoute, Router} from "@angular/router";
import {DtoCombinerService} from "../../services/dto-combiner.service";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CategoryDto} from "../../dtos/category-dto.model";
import {DetailPageComponent} from "../../shared/modules/detail-page.component";
import {HelperService} from "../../services/helper.service";

@Component({
  selector: 'app-category-detail',
  templateUrl: './category-detail.component.html',
  styleUrls: ['./category-detail.component.css']
})
export class CategoryDetailComponent extends DetailPageComponent<CategoryTemplateDto> implements OnInit {

  // CONFIG
  tableColumns = ['title', 'shortcut', 'faceToFace', 'actions']

  // VARs
  editCategory = new CategoryDto();

  // FORMs
  infoForm = new FormGroup({
    title: new FormControl(
      { value:'', disabled: false },
      Validators.compose([Validators.required, Validators.minLength(1)])),
    description: new FormControl( { value:'', disabled: false } ),
    withoutClient: new FormControl(false)
  })

  editCategoryForm = new FormGroup({
    title: new FormControl(),
    shortcut: new FormControl(),
    description: new FormControl( { value:'', disabled: false } ),
    faceToFace: new FormControl(true)
  })

  get titleControl() { return this.infoForm.controls['title']; }

  get descriptionControl() { return this.infoForm.controls['description']; }

  get withoutClientControl() { return this.infoForm.controls['withoutClient']; }

  get categoryTitleControl() { return this.editCategoryForm.controls['title']; }

  get categoryShortcutControl() { return this.editCategoryForm.controls['shortcut']; }

  get categoryDescriptionControl() { return this.editCategoryForm.controls['description']; }

  get categoryFaceToFaceControl() { return this.editCategoryForm.controls['faceToFace']; }

  constructor(
    override helperService: HelperService,
    private categoryService: CategoriesService,
    private dtoCombinerService: DtoCombinerService,
    private router: Router,
    private route: ActivatedRoute,
    private modalService: NgbModal
    ) {
    super(helperService);
  }

  loadValues() {
    // get id
    const id = this.route.snapshot.paramMap.get('id');

    if (id != null) {
      this.categoryService
        .getById(+id)
        .subscribe({
          next: (value) => {
            this.value = value;
            this.value$.next(value);
            this.editValue = <CategoryTemplateDto> {... value};

            this.refreshForm();
          }
        })
    }
  }

  refreshForm() {
    this.titleControl.setValue(this.editValue.title);
    this.descriptionControl.setValue(this.editValue.description);
    this.withoutClientControl.setValue(this.editValue.withoutClient);
  }

  refreshCategoryForm() {
    this.categoryTitleControl.setValue(this.editCategory.title);
    this.categoryShortcutControl.setValue(this.editCategory.shortcut);
    this.categoryDescriptionControl.setValue(this.editCategory.description);
    this.categoryFaceToFaceControl.setValue(this.editCategory.faceToFace);
  }

  initFormSubscriptions() {
    this.titleControl.valueChanges.subscribe(value => this.editValue.title = value);
    this.descriptionControl.valueChanges.subscribe(value => this.editValue.description = value);
    this.withoutClientControl.valueChanges.subscribe(value => this.editValue.withoutClient = value);

    this.categoryTitleControl.valueChanges.subscribe(value => this.editCategory.title = value);
    this.categoryShortcutControl.valueChanges.subscribe(value => this.editCategory.shortcut = value);
    this.categoryDescriptionControl.valueChanges.subscribe(value => this.editCategory.description = value);
    this.categoryFaceToFaceControl.valueChanges.subscribe(value => this.editCategory.faceToFace = value);
  }

  getNewValue(): CategoryTemplateDto {
    return new CategoryTemplateDto();
  }

  update() {
    if (this.isSubmitting)
      return;

    this.categoryService
      .update(this.editValue.id, this.editValue)
      .subscribe({
        next: () => this.handleSuccess("Kategorienvorlage gespeichert"),
        error: () => this.handleFailure("Fehler beim speichern")});
  }

  deleteCategory(category: CategoryDto) {
    this.editValue.categories = this.editValue.categories.filter(x => x !== category)

    this.update()
  }

  openCategoryEditModal(content, categoryDto: CategoryDto | null) {
    if (categoryDto != null)
      this.editCategory = <CategoryDto> {...categoryDto}
    else {
      this.editCategory = new CategoryDto()
    }

    this.refreshCategoryForm();

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-title', scrollable: true })
      .result
      .then((result: Boolean) => {
        if (result) {
          // get changes
          if (categoryDto != null) {
            categoryDto.title = this.editCategory.title;
            categoryDto.shortcut = this.editCategory.shortcut;
            categoryDto.description = this.editCategory.description;
            categoryDto.faceToFace = this.editCategory.faceToFace;
          } else {
            this.value.categories.push(this.editCategory)
          }

          this.update()
        }
      })
      .catch();
  }

  openDeleteConfirmationModal(content, value: CategoryDto) {
    this.modalService
      .open(content, {ariaLabelledBy: 'modal-basic-title', scrollable: true})
      .result
      .then((result: Boolean) => {
        if (result) {
          this.deleteCategory(value);
        }
      });
  }
}
