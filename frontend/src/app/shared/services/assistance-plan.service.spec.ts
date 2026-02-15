import {of} from 'rxjs';
import {describe, expect, it, vi} from 'vitest';
import {environment} from '../../../environments/environment';
import {AssistancePlanUpdateDto} from '../dtos/assistance-plan-update-dto.model';
import {AssistancePlanService} from './assistance-plan.service';

describe('AssistancePlanService', () => {
  it('updateWithCreateLikeDto sends update dto to assistance plan endpoint', () => {
    const put = vi.fn().mockReturnValue(of({}));
    const service = new AssistancePlanService({put} as any);

    const dto = new AssistancePlanUpdateDto();
    dto.id = 55;
    dto.clientId = 9;
    dto.institutionId = 4;
    dto.sponsorId = 3;

    service.updateWithCreateLikeDto(55, dto).subscribe();

    expect(put).toHaveBeenCalledWith(`${environment.api_url}assistance_plans/55`, dto);
  });

  it('getPreviewByClientId calls client preview endpoint', () => {
    const get = vi.fn().mockReturnValue(of([]));
    const service = new AssistancePlanService({get} as any);

    service.getPreviewByClientId(7).subscribe();

    expect(get).toHaveBeenCalledWith(`${environment.api_url}assistance_plans/client/7/preview`);
  });

  it('getPreviewByInstitutionId calls institution preview endpoint', () => {
    const get = vi.fn().mockReturnValue(of([]));
    const service = new AssistancePlanService({get} as any);

    service.getPreviewByInstitutionId(8).subscribe();

    expect(get).toHaveBeenCalledWith(`${environment.api_url}assistance_plans/institution/8/preview`);
  });

  it('getPreviewBySponsorId calls sponsor preview endpoint', () => {
    const get = vi.fn().mockReturnValue(of([]));
    const service = new AssistancePlanService({get} as any);

    service.getPreviewBySponsorId(9).subscribe();

    expect(get).toHaveBeenCalledWith(`${environment.api_url}assistance_plans/sponsor/9/preview`);
  });

  it('getPreviewByFavorites calls favorites preview endpoint', () => {
    const get = vi.fn().mockReturnValue(of([]));
    const service = new AssistancePlanService({get} as any);

    service.getPreviewByFavorites().subscribe();

    expect(get).toHaveBeenCalledWith(`${environment.api_url}assistance_plans/favorites/preview`);
  });
});
