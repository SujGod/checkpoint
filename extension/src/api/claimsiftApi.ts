import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import type {
  FactCheckRequest,
  FactCheckResponse,
  ProcessVideoResponse,
} from "../types/fact-check";
import type {
  ExtractClaimsResponse,
  ExtractClaimsRequest,
  ProcessVideoRequest,
} from "../types/transcript";

export const claimsiftApi = createApi({
  reducerPath: "claimsiftApi",

  baseQuery: fetchBaseQuery({
    baseUrl: "http://localhost:8080/api/v1",
  }),

  endpoints: (builder) => ({
    checkClaim: builder.mutation<FactCheckResponse, FactCheckRequest>({
      query: (request: FactCheckRequest) => ({
        url: "/checks",
        method: "POST",
        body: request,
      }),
    }),
    extractClaims: builder.mutation<
      ExtractClaimsResponse,
      ExtractClaimsRequest
    >({
      query: (request) => ({
        url: "/claims/extract",
        method: "POST",
        body: request,
      }),
    }),
    processVideo: builder.mutation<ProcessVideoResponse, ProcessVideoRequest>({
      query: (request) => ({
        url: "/videos/process",
        method: "POST",
        body: request,
      }),
    }),
  }),
});

export const {
  useCheckClaimMutation,
  useExtractClaimsMutation,
  useProcessVideoMutation,
} = claimsiftApi;
