import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {ClientArchiveExportFormat} from "../../../shared/dtos/client-archive-export-format.model";

export class ClientArchiveExportForm extends UntypedFormGroup {
  constructor() {
    super({
      format: new UntypedFormControl(ClientArchiveExportFormat.JSON, Validators.required),
      anonymize: new UntypedFormControl(false, Validators.required)
    });
  }

  get format() { return this.controls['format']; }
  get anonymize() { return this.controls['anonymize']; }
}
