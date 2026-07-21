import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";

import type { ProcessVideoResponse } from "../types/fact-check";
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

export const { useExtractClaimsMutation, useProcessVideoMutation } =
  claimsiftApi;
