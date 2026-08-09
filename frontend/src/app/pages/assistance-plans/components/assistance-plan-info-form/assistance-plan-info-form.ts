import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {AssistancePlanHourMode} from "../../../../shared/dtos/assistance-plan-hour-mode.model";

export class AssistancePlanInfoForm extends UntypedFormGroup {
  constructor(withoutInstitution: boolean = false) {
    super({
      start: new UntypedFormControl(Date.now(), Validators.compose([Validators.required])),
      end: new UntypedFormControl(Date.now(), Validators.compose([Validators.required])),
      sponsor: new UntypedFormControl(null, Validators.compose([Validators.required])),
      hourMode: new UntypedFormControl(AssistancePlanHourMode.EXACT, Validators.compose([Validators.required])),
      hourCorridor: new UntypedFormControl(null),
      institution: new UntypedFormControl(
        null,
        withoutInstitution ? null : Validators.compose([Validators.required]))
    });
  }

  get start() { return this.controls['start']; }

  get end() { return this.controls['end']; }

  get sponsor() { return this.controls['sponsor']; }

  get hourMode() { return this.controls['hourMode']; }

  get hourCorridor() { return this.controls['hourCorridor']; }

  get institution() { return this.controls['institution']; }
}
