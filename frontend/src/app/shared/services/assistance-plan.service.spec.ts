import {of} from 'rxjs';
import {describe, expect, it, vi} from 'vitest';
import {environment} from '../../../environments/environment';
import {AssistancePlanUpdateDto} from '../dtos/assistance-plan-update-dto.model';
import {AssistancePlanService} from './assistance-plan.service';

describe('AssistancePlanService', () => {
  it('updateWithCreateLikeDto sends update dto to assistance plan endpoint', () => {
    const put = vi.fn().mockReturnValue(of({}));
    const service = new AssistancePlanService(
      {put} as any,
      {} as any,
      {} as any,
      {} as any,
      {} as any
    );

    const dto = new AssistancePlanUpdateDto();
    dto.id = 55;
    dto.clientId = 9;
    dto.institutionId = 4;
    dto.sponsorId = 3;

    service.updateWithCreateLikeDto(55, dto).subscribe();

    expect(put).toHaveBeenCalledWith(`${environment.api_url}assistance_plans/55`, dto);
  });
});
