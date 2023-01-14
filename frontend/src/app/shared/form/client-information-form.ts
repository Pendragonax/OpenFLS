import {FormControl, FormGroup, Validators} from "@angular/forms";

export class ClientInformationForm extends FormGroup {
  constructor() {
    super({
      firstName: new FormControl({value:'', disabled: false}, Validators.compose([
        Validators.required,
        Validators.minLength(1)])),
      lastName: new FormControl({value:'', disabled: false}, Validators.compose([
        Validators.required,
        Validators.minLength(1)])),
      phone: new FormControl(''),
      email: new FormControl('', Validators.compose([
        Validators.pattern("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")
      ])),
      institution: new FormControl(""),
      categoryTemplate: new FormControl("")});
  }

  get firstName() { return this.controls['firstName']; }

  get lastName() { return this.controls['lastName']; }

  get phone() { return this.controls['phone']; }

  get email() { return this.controls['email']; }

  get institution() { return this.controls['institution']; }

  get categoryTemplate() { return this.controls['categoryTemplate']; }
}
