import {Component, OnInit} from '@angular/core';
import {CategoryTemplateDto} from "../../../../shared/dtos/category-template-dto.model";
import {AbstractControl, UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CategoriesService} from "../../../../shared/services/categories.service";
import {Router} from "@angular/router";
import {CategoryDto} from "../../../../shared/dtos/category-dto.model";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {MatTableDataSource} from "@angular/material/table";

@Component({
    selector: 'app-category-new',
    templateUrl: './category-new.component.html',
    styleUrls: ['./category-new.component.css'],
    standalone: false
})
export class CategoryNewComponent implements OnInit {

  // states
  isSubmitting = false;

  displayedColumns = ['title', 'shortcut', 'faceToFace', 'actions']

  value = new CategoryTemplateDto()
  editableCategory = new CategoryDto()

  tableDataSource = new MatTableDataSource<CategoryDto>()

  infoForm = new UntypedFormGroup({
    title: new UntypedFormControl(
      { value:'', disabled: false },
      Validators.compose([Validators.required, Validators.minLength(1)])),
    description: new UntypedFormControl( { value:'', disabled: false } ),
    withoutClient: new UntypedFormControl(false)
  })
  editCategoryForm = new UntypedFormGroup({
    title: new UntypedFormControl(),
    shortcut: new UntypedFormControl(),
    faceToFace: new UntypedFormControl(true)
  })

  public get title() { return this.infoForm.get('title'); }

  public get description() { return this.infoForm.get('description'); }

  public get withoutClient() { return this.infoForm.get('withoutClient'); }

  public get categoryTitle() { return this.editCategoryForm.get('title'); }

  public get categoryShortcut() { return this.editCategoryForm.get('shortcut'); }

  constructor(
    private snackBar: MatSnackBar,
    private categoryService: CategoriesService,
    private router: Router,
    private modalService: NgbModal) { }

  ngOnInit(): void {
    this.updateTableDatasource()
  }

  save() {
    this.isSubmitting = true;

    this.fillCategoryTemplate()

    this.categoryService
      .create(this.value)
      .subscribe({
        next: (categoryTemplate) => {
          this.value = categoryTemplate;
          this.handleSuccess() },
        error: (err) => this.handleFailure(err)
      });
  }

  handleSuccess() {
    this.isSubmitting = false;

    // redirect after success
    this.router.navigate([`/category/detail/${this.value.id}`]).then();
  }

  handleFailure(err: any) {
    console.log(err);
    this.isSubmitting = false;
    this.snackBar.open("Fehler beim speichern", "", { duration: 750})
  }

  private fillCategoryTemplate() {
    this.value.title = this.title?.value;
    this.value.description = this.description?.value;
    this.value.withoutClient = this.withoutClient?.value === true;
  }

  updateTableDatasource() {
    this.tableDataSource.data = this.value.categories
    console.log("updated")
  }

  setEditableTitle(value) {
    this.editableCategory.title = value
  }

  setEditableShortcut(value) {
    this.editableCategory.shortcut = value
  }

  deleteCategory(categoryDto: CategoryDto) {
    this.value.categories = this.value.categories.filter((it) => it != categoryDto)
    this.updateTableDatasource()
  }

  open(content, categoryDto: CategoryDto | null) {
    if (categoryDto != null)
      this.editableCategory = <CategoryDto> {...categoryDto}
    else {
      this.editableCategory = new CategoryDto()
    }

    // fill form
    this.editCategoryForm.get('title')?.setValue(this.editableCategory.title)
    this.editCategoryForm.get('shortcut')?.setValue(this.editableCategory.shortcut)

    this.modalService
      .open(content, { ariaLabelledBy: 'modal-basic-title', scrollable: true })
      .result
      .then((result: Boolean) => {
        if (result) {
          if (categoryDto != null) {
            categoryDto.title = this.editableCategory.title;
            categoryDto.shortcut = this.editableCategory.shortcut;
            categoryDto.faceToFace = this.editableCategory.faceToFace;
          } else {
            this.value.categories.push(this.editableCategory)
          }

          this.updateTableDatasource()
        }
      });
  }

  getControlErrorMessage(control: AbstractControl | null) {
    if (control?.hasError('required')) {
      return 'Eingabe ist notwendig';
    }
    if (control?.hasError('minlength')) {
      return 'zu wenig Zeichen';
    }
    if (control?.hasError('pattern')) {
      return 'Großbuchtabe, Zahl und Sonderzeichen notwendig';
    }

    return 'unbekannter Fehler';
  }
}
