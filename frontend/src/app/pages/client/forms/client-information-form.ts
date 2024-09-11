import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";

export class ClientInformationForm extends UntypedFormGroup {
  constructor() {
    super({
      firstName: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
        Validators.required,
        Validators.minLength(1)])),
      lastName: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
        Validators.required,
        Validators.minLength(1)])),
      phone: new UntypedFormControl(''),
      email: new UntypedFormControl('', Validators.compose([
        Validators.pattern("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")
      ])),
      institution: new UntypedFormControl(""),
      categoryTemplate: new UntypedFormControl("")});
  }

  get firstName() { return this.controls['firstName']; }

  get lastName() { return this.controls['lastName']; }

  get phone() { return this.controls['phone']; }

  get email() { return this.controls['email']; }

  get institution() { return this.controls['institution']; }

  get categoryTemplate() { return this.controls['categoryTemplate']; }
}
