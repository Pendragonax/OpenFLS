import {CategoryDto} from "./category-dto.model";

export class CategoryTemplateDto {
  id: number = 0;
  title: string = "";
  withoutClient: boolean = true;
  description: string = "";
  categories: CategoryDto[] = [];
}
