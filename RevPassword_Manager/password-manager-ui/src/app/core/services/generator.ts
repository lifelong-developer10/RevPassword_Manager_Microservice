import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GenerateRequest {
  length: number;
  uppercase: boolean;
  lowercase: boolean;
  numbers: boolean;
  symbols: boolean;
  excludeSimilar: boolean;
  count: number;
}

export interface GenerateResponse {
  passwords: string[];
}

@Injectable({
  providedIn: 'root'
})
export class GeneratorService {

  private API = 'http://localhost:8080/api/generator';

  constructor(private http: HttpClient) {}

  /**
   * Generate passwords using the backend generator-service.
   * Calls POST /api/generator/generate with full options.
   */
  generate(options: GenerateRequest): Observable<GenerateResponse> {
    return this.http.post<GenerateResponse>(`${this.API}/generate`, options);
  }

  /**
   * Quick generate with just a length param (GET endpoint).
   */
  quickGenerate(length: number = 12): Observable<GenerateResponse> {
    const params = new HttpParams().set('length', length.toString());
    return this.http.get<GenerateResponse>(`${this.API}/generate`, { params });
  }
}
