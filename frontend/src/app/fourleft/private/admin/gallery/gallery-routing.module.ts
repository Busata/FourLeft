import {RouterModule, Routes} from "@angular/router";
import {NgModule} from "@angular/core";

import {GalleryPageComponent} from "./gallery-page/gallery-page.component";
import {GalleryHomeComponent} from "./gallery-home/gallery-home.component";

const routes: Routes = [
  {
    path: '',
    component: GalleryPageComponent,
    children: [      {
      path: '',
      component: GalleryHomeComponent,

    },]
  }
]


@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class GalleryRoutingModule {}
