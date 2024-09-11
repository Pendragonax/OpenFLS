import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";

export class InstitutionInfoForm extends UntypedFormGroup {
  constructor() {
    super({
      name: new UntypedFormControl({value:'', disabled: false}, Validators.compose([
        Validators.required,
        Validators.minLength(1)])),
      phone: new UntypedFormControl(''),
      email: new UntypedFormControl('', Validators.compose([
        Validators.pattern("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$")
      ]))});
  }

  get name() { return this.controls['name']; }

  get phone() { return this.controls['phone']; }

  get email() { return this.controls['email']; }
}
