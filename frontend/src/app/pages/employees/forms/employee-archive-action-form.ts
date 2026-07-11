import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";

export class EmployeeArchiveActionForm extends UntypedFormGroup {
  constructor() {
    super({
      actionDate: new UntypedFormControl(new Date(), Validators.required),
      reason: new UntypedFormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(1)
      ])),
      remark: new UntypedFormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(1)
      ]))
    });
  }

  get actionDate() { return this.controls['actionDate']; }
  get reason() { return this.controls['reason']; }
  get remark() { return this.controls['remark']; }
}
