import { Injectable, Optional } from '@angular/core';
import {OAuthModuleConfig, OAuthResourceServerErrorHandler, OAuthStorage} from 'angular-oauth2-oidc';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';

import {Observable} from 'rxjs';

@Injectable()
export class Oauth2Interceptor implements HttpInterceptor {

  constructor(
    private authStorage: OAuthStorage,
    private errorHandler: OAuthResourceServerErrorHandler,
    @Optional() private moduleConfig: OAuthModuleConfig
  ) {
  }

  private checkUrl(url: string): boolean {
    return url.indexOf("/api/internal") !== -1;
  }

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    let url = req.url.toLowerCase();

    if (!this.checkUrl(url)) return next.handle(req);

    let token = this.authStorage.getItem('access_token');
    let header = `Bearer ${token}`;

    let headers = req.headers.set('Authorization', header);

    req = req.clone({ headers });

    return next.handle(req);

  }

}
